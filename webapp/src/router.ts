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
import Router from "vue-router";
import Home from "./components/Home.vue";

Vue.use(Router);
/**
 * This router maps which components should be displayed for different paths within the application. In this example app, the only component is the Home component,
 * which is contained in the Home.vue file. When a user navigates to this application, this router.ts file tells Vue to populate the <router-view/> tag from the App.vue
 * file with the <template>, <script>, and <style> defined inside the Home component.
 */
export default new Router({
  base: process.env.BASE_URL,
  routes: [
    {
      path: "/",
      name: "home",
      component: Home,
    },
    {
      path: "*",
      redirect: "/",
    },
  ],
});
