import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
interface Mascota {
  id?: number;
  codigo: string;
  nombre: string;
  especie: string;
  raza: string;
  edadMeses: number;
  sexo: string;
  tamanio: string;
  descripcion: string;
  vacunada: boolean;
  esterilizada: boolean;
  estado: string;
}
@Injectable({ providedIn: 'root' })
export class AdoptaService {
  private readonly router = inject(Router);
  usuario = signal('');
  mascotas = signal<Mascota[]>([]);
  cargando = signal(true);
  ocupado = signal(false);
  error = signal('');
  aviso = signal('');
  editor = signal(false);
  eliminando = signal<Mascota | null>(null);
  username = '';
  password = '';
  buscar = '';
  filtro = '';
  formulario = this.vacia();
  private csrf = { token: '', headerName: '' };
  vacia(): Mascota {
    return {
      codigo: '',
      nombre: '',
      especie: 'PERRO',
      raza: '',
      edadMeses: 0,
      sexo: 'HEMBRA',
      tamanio: 'MEDIANO',
      descripcion: '',
      vacunada: false,
      esterilizada: false,
      estado: 'DISPONIBLE',
    };
  }
  async comprobarSesion() {
    try {
      await this.token();
      const r = await fetch('/api/me');
      if (r.ok) {
        this.usuario.set((await this.leerJson(r)).username);
      } else if (r.status === 401) {
        this.usuario.set('');
      } else throw new Error('No se pudo conectar con el servidor.');
    } catch (e) {
      this.usuario.set('');
      this.mostrarError(e);
    } finally {
      this.cargando.set(false);
    }
  }
  private async token() {
    const r = await fetch('/api/csrf');
    if (!r.ok) throw new Error('No se pudo conectar con el backend. Comprueba que esté iniciado.');
    this.csrf = await this.leerJson(r);
  }
  private async leerJson(response: Response) {
    if (!response.headers.get('Content-Type')?.includes('application/json')) {
      throw new Error(
        'La conexión con el backend no está configurada. Reinicia Angular con npm start desde frontend.',
      );
    }
    return response.json();
  }
  private async solicitar(url: string, method = 'GET', body?: unknown) {
    const headers: Record<string, string> = {};
    if (method !== 'GET') {
      headers[this.csrf.headerName] = this.csrf.token;
      headers['Content-Type'] = 'application/json';
    }
    const r = await fetch(url, { method, headers, body: body ? JSON.stringify(body) : undefined });
    if (!r.ok) {
      if (r.status === 401) {
        this.usuario.set('');
        this.editor.set(false);
        this.eliminando.set(null);
        this.mascotas.set([]);
        void this.router.navigateByUrl('/login');
        throw new Error('La sesión terminó. Ingresa nuevamente.');
      }
      const data = await r.json().catch(() => ({}));
      throw new Error(data.message || 'No se pudo completar la operación.');
    }
    return r.status === 204 ? null : this.leerJson(r);
  }
  async ingresar() {
    this.ocupado.set(true);
    this.error.set('');
    try {
      await this.token();
      const r = await fetch('/api/login', {
        method: 'POST',
        headers: {
          [this.csrf.headerName]: this.csrf.token,
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({ username: this.username, password: this.password }),
      });
      if (!r.ok)
        throw new Error(
          r.status === 401 ? 'Usuario o contraseña incorrectos.' : 'No se pudo iniciar sesión.',
        );
      this.password = '';
      await this.token();
      this.usuario.set((await this.solicitar('/api/me')).username);
      await this.router.navigateByUrl('/mascotas');
    } catch (e) {
      this.mostrarError(e);
    } finally {
      this.ocupado.set(false);
    }
  }
  async salir() {
    this.ocupado.set(true);
    try {
      await this.solicitar('/api/logout', 'POST');
      this.usuario.set('');
      this.mascotas.set([]);
      this.editor.set(false);
      this.eliminando.set(null);
      this.aviso.set('');
      this.error.set('');
      this.buscar = '';
      this.filtro = '';
      await this.router.navigateByUrl('/login');
    } catch (e) {
      this.mostrarError(e);
    } finally {
      this.ocupado.set(false);
    }
  }
  async cargar() {
    this.cargando.set(true);
    try {
      this.mascotas.set(await this.solicitar('/api/mascotas'));
    } catch (e) {
      this.mostrarError(e);
    } finally {
      this.cargando.set(false);
    }
  }
  visibles() {
    const t = this.buscar.toLocaleLowerCase();
    return this.mascotas().filter(
      (m) =>
        (!this.filtro || m.estado === this.filtro) &&
        `${m.nombre} ${m.codigo} ${m.especie} ${m.raza}`.toLocaleLowerCase().includes(t),
    );
  }
  disponibles() {
    return this.mascotas().filter((m) => m.estado === 'DISPONIBLE').length;
  }
  abrir(m?: Mascota) {
    this.formulario = m ? { ...m } : this.vacia();
    this.error.set('');
    this.editor.set(true);
  }
  async guardar() {
    this.ocupado.set(true);
    this.error.set('');
    try {
      const id = this.formulario.id;
      await this.solicitar(
        '/api/mascotas' + (id ? '/' + id : ''),
        id ? 'PUT' : 'POST',
        this.formulario,
      );
      this.editor.set(false);
      this.aviso.set(id ? 'Mascota actualizada.' : 'Mascota registrada.');
      await this.cargar();
    } catch (e) {
      this.mostrarError(e);
    } finally {
      this.ocupado.set(false);
    }
  }
  async eliminar() {
    const m = this.eliminando();
    if (!m) return;
    this.ocupado.set(true);
    this.error.set('');
    try {
      await this.solicitar('/api/mascotas/' + m.id, 'DELETE');
      this.eliminando.set(null);
      this.aviso.set('Mascota eliminada.');
      await this.cargar();
    } catch (e) {
      this.mostrarError(e);
    } finally {
      this.ocupado.set(false);
    }
  }
  etiqueta(e: string) {
    return e === 'EN_PROCESO' ? 'En proceso' : e === 'ADOPTADA' ? 'Adoptada' : 'Disponible';
  }
  icono(e: string) {
    return e === 'PERRO' ? '🐶' : e === 'GATO' ? '🐱' : e === 'CONEJO' ? '🐰' : '🐾';
  }
  private mostrarError(e: unknown) {
    this.error.set(e instanceof Error ? e.message : 'Error de conexión.');
  }
}
