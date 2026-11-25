import { Routes } from '@angular/router';
import { Login } from './login';
import { Mascotas } from './mascotas';
import { authGuard, invitadoGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitadoGuard], title: 'Ingresar | Adopta' },
  {
    path: 'mascotas', component: Mascotas, canActivate: [authGuard],
    runGuardsAndResolvers: 'always', title: 'Mascotas | Adopta',
  },
  { path: '', redirectTo: 'mascotas', pathMatch: 'full' },
  { path: '**', redirectTo: 'mascotas' },
];
