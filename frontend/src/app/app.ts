import { Component } from '@angular/core';

import { PreapprovedUsagePage } from './features/preapproved-usage/preapproved-usage-page';

@Component({
  selector: 'app-root',
  imports: [PreapprovedUsagePage],
  template: '<app-preapproved-usage />'
})
export class App {}
