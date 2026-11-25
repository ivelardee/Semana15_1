import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { routes } from './app.routes';
import { AdoptaService } from './adopta.service';

describe('Navegación y sesión', () => {
  let autenticado: boolean;
  beforeEach(() => {
    autenticado = false;
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string) => {
        const json = (value: unknown, status = 200) =>
          new Response(JSON.stringify(value), {
            status,
            headers: { 'Content-Type': 'application/json' },
          });
        if (url === '/api/csrf') return json({ token: 'test', headerName: 'X-CSRF-TOKEN' });
        if (url === '/api/me') return autenticado ? json({ username: 'admin' }) : json({}, 401);
        if (url === '/api/login') {
          autenticado = true;
          return new Response(null, { status: 204 });
        }
        if (url === '/api/logout') {
          autenticado = false;
          return new Response(null, { status: 204 });
        }
        if (url === '/api/mascotas') return autenticado ? json([]) : json({}, 401);
        return json({}, 404);
      }),
    );
  });
  afterEach(() => vi.unstubAllGlobals());

  it('redirige el acceso directo sin sesión al login', async () => {
    const harness = await RouterTestingHarness.create('/mascotas');
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(harness.routeNativeElement?.textContent).toContain('Iniciar sesión');
  });

  it('restaura la sesión al abrir directamente mascotas', async () => {
    autenticado = true;
    const harness = await RouterTestingHarness.create('/mascotas');
    expect(TestBed.inject(Router).url).toBe('/mascotas');
    expect(harness.routeNativeElement?.textContent).toContain('Registrar mascota');
  });

  it('navega después del login y protege el panel después del logout', async () => {
    const harness = await RouterTestingHarness.create('/login');
    const state = TestBed.inject(AdoptaService);
    state.username = 'admin';
    state.password = 'prueba';
    await state.ingresar();
    expect(TestBed.inject(Router).url).toBe('/mascotas');
    await state.salir();
    expect(TestBed.inject(Router).url).toBe('/login');
    await harness.navigateByUrl('/mascotas');
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('redirige la raíz y las rutas desconocidas según la sesión', async () => {
    autenticado = true;
    const harness = await RouterTestingHarness.create('/');
    expect(TestBed.inject(Router).url).toBe('/mascotas');
    await harness.navigateByUrl('/login');
    expect(TestBed.inject(Router).url).toBe('/mascotas');
    autenticado = false;
    await harness.navigateByUrl('/no-existe');
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('regresa al login cuando expira la sesión durante una consulta', async () => {
    autenticado = true;
    const harness = await RouterTestingHarness.create('/mascotas');
    autenticado = false;
    const state = TestBed.inject(AdoptaService);
    await state.cargar();
    await harness.fixture.whenStable();
    expect(state.usuario()).toBe('');
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(state.error()).toContain('sesión terminó');
  });
});
