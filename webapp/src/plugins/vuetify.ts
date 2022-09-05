/*
 * Copyright 2019-2020 by Security and Safety Things GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import Vue from "vue";
import Vuetify from "vuetify/lib";
/**
 * Needed for any text that is put on any of the web pages
 */
import "roboto-fontface/css/roboto/roboto-fontface.css";
import { COLOR } from "@/services/color";
Vue.use(Vuetify);
/**
 * Sets up Vue to use Vuetify
 */
export default new Vuetify({
  theme: {
    themes: {
      light: {
        primary: COLOR.primary,
        secondary: COLOR.accent,
        accent: COLOR.accent,
        error: COLOR.error,
        info: COLOR.info,
        success: COLOR.success,
        warning: COLOR.warning,
        grey: COLOR.grey,
        black: COLOR.black,
      },
    },
  },
  icons: {
    iconfont: "mdiSvg",
  },
  breakpoint: {
    thresholds: {
      xs: 720, // Override the default xs value (600). This breakpoint is used in the App component.
    },
  },
  options: { customProperties: true }, // makes it possible to use the colors above in CSS like: "color: var(--v-primary-base);"
});
