<!--
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
-->

<!-- The template element contains the HTML code -->
<template>
  <v-app>
    <!-- The colon is shorthand for the v-bind directive. This directive is used for property binding. 
    https://vuejs.org/v2/api/#v-bind 
    Here, the title prop of Toolbar is bound to the title property and the icon prop is bound to the appIcon property inside the App object below. -->
    <Toolbar :title="title" :icon="appIcon" />
    <v-main>
      <!-- Change padding dynamically based on the window size. -->
      <div :style="{ width: '100%', padding: '0 ' + ($vuetify.breakpoint.xsOnly ? '24px' : '48px') }">
        <!-- The router-view is populated with the Home.vue component when the app starts. -->
        <router-view />
      </div>
    </v-main>
    <Footer />
  </v-app>
</template>

<!-- The script element contains the Typescript code -->
<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { Toolbar, Footer } from "@azena/jumpsuite";
import AppIcon from "@/assets/images/appicon.svg";

@Component({
  components: { Toolbar, Footer },
})
export default class App extends Vue {
  private title = "";
  private appIcon: string = AppIcon;

  /**
   * Vue lifecycle hook that executes before the component DOM has been mounted.
   */
  beforeMount(): void {
    /*
     * Checks if .env file contained in the root directory of webapp contains the title of the web
     * You can find more about Vue environment variable here https://cli.vuejs.org/guide/mode-and-env.html#environment-variables
     */
    this.title = process.env.VUE_APP_WEB_PAGE_TITLE;
  }
}
</script>

<!-- The style element contains the SCSS styles -->
<style scoped lang="scss"></style>
