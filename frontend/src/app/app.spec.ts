import { AdoptaService } from './adopta.service';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { describe, it, expect, vi } from 'vitest';

describe('Conexión con el backend', () => {
  it('explica el proxy ausente cuando Angular devuelve HTML', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response('<!doctype html><html></html>', {
          status: 200,
          headers: { 'Content-Type': 'text/html' },
        }),
      ),
    );
    try {
      TestBed.configureTestingModule({ providers: [provideRouter([])] });
      const app = TestBed.inject(AdoptaService);
      await app.comprobarSesion();
      expect(app.error()).toContain('Reinicia Angular');
      expect(app.usuario()).toBe('');
      expect(app.cargando()).toBe(false);
    } finally {
      vi.unstubAllGlobals();
    }
  });
});
describe('Filtro de mascotas', () => {
  it('combina búsqueda y estado', () => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const app = TestBed.inject(AdoptaService);
    app.mascotas.set([
      { ...app.vacia(), id: 1, nombre: 'Luna' },
      { ...app.vacia(), id: 2, nombre: 'Milo', estado: 'ADOPTADA' },
    ]);
    app.buscar = 'LUNA';
    expect(app.visibles().map((m) => m.id)).toEqual([1]);
    app.filtro = 'ADOPTADA';
    expect(app.visibles()).toEqual([]);
    app.buscar = '';
    expect(app.visibles().map((m) => m.id)).toEqual([2]);
    expect(app.mascotas()).toHaveLength(2);
  });
});
