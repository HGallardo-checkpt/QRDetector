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
  <!-- mx-auto and my-11 are Vuetify's spacing helper classes. https://vuetifyjs.com/en/styles/spacing/#how-it-works
  mx-auto means margin-left and margin-right are set to auto, my-12 means margin-top and margin-bottom are set to 12*4px = 48px -->
  <div style="width: 100%; max-width: 1440px" class="mx-auto my-12">
    <!-- text-h1 is a Vuetify Typography helper class that sets the size and style of text according to Material Design.
    https://vuetifyjs.com/en/styles/text-and-typography/#typography -->
    <div class="text-h1 mb-6">Objective</div>
    <div class="text-body-1 mb-16">Retrieve an image from the VideoPipeline, and display the image on a Web UI.</div>
    <div class="text-h1 mb-6">Live stream</div>
    <v-alert :value="liveViewError" class="mb-6" type="error" dense text icon="$cancel">
      Unable to acquire video stream!
    </v-alert>
    <img width="100%" :src="liveViewUrl" />
  </div>
</template>

<!-- The script element contains the Typescript code -->
<script lang="ts">
import Vue from "vue";
import axios from "axios";

const Home = Vue.extend({
  /* Maps a new vue component called home to this file */
  name: "Home",
  /* Register the component member variables */
  data: () => ({
    /* Points to the api/example/live endpoint */
    liveViewUrl: "",
    /* If the api/example/live endpoint fails to obtain an image, liveViewError is set to true, and the <v-alert> tag is displayed. */
    liveViewError: false,
  }),
  /**
   * Vue lifecycle hook for when this component has been mounted to the DOM.
   */
  mounted(): void {
    /*
     * This function attempts to obtain a response from the api/example/hello-world endpoint.
     * This endpoint is just used for development purposes as a simple way to verify a connection to the webserver.
     */
    axios
      .get(`${process.env.VUE_APP_REST_PATH_PREFIX}/example/hello-world`)
      .then((response) => {
        console.log(response.data);
      })
      .catch((error) => {
        if (error.request) {
          this.liveViewError = true;
        }
      });
    /*
     * Calls the retrieveImage() function, which obtains an image from the api/example/live endpoint, and continuously calls itself to update the image being displayed
     * in the <img> tag
     */
    this.retrieveImage();
  },
  /* Register the component methods that are used during its lifecycle */
  methods: {
    /**
     * Request a new image from the HelloWorldEndpoint by using the REST API endpoint path and the current timestamp as the url.
     * If it succeeds we display the image retrieved in our <img> in the template. Otherwise, we retry the request in 500ms.
     */
    retrieveImage(): void {
      const url = `${process.env.VUE_APP_REST_PATH_PREFIX}/example/live?time=${Date.now()}`;
      const img = new Image();
      img.onload = () => {
        this.liveViewUrl = url;
        this.liveViewError = false;
        window.requestAnimationFrame(this.retrieveImage);
      };
      img.onerror = () => {
        this.liveViewError = true;
        /**
         * If we encounter error screen, retry in 500 ms
         */
        setTimeout(() => this.retrieveImage(), 500);
      };
      img.src = url;
    },
  },
});

export default Home;
</script>

<!-- The style element contains the SCSS styles -->
<style scoped lang="scss"></style>
