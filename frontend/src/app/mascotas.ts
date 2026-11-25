import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdoptaService } from './adopta.service';

@Component({
  selector: 'app-mascotas',
  imports: [FormsModule],
  templateUrl: './mascotas.html',
  styleUrl: './app.css',
})
export class Mascotas implements OnInit {
  readonly vm = inject(AdoptaService);
  ngOnInit() {
    void this.vm.cargar();
  }
}
