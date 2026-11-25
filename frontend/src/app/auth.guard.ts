import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AdoptaService } from './adopta.service';

export const authGuard: CanActivateFn = async () => {
  const sesion = inject(AdoptaService);
  const router = inject(Router);
  await sesion.comprobarSesion();
  return sesion.usuario() ? true : router.createUrlTree(['/login']);
};

export const invitadoGuard: CanActivateFn = async () => {
  const sesion = inject(AdoptaService);
  const router = inject(Router);
  await sesion.comprobarSesion();
  return sesion.usuario() ? router.createUrlTree(['/mascotas']) : true;
};
