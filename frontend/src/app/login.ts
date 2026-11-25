import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdoptaService } from './adopta.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './app.css',
})
export class Login {
  readonly vm = inject(AdoptaService);
}
