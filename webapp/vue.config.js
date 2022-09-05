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
module.exports = {
  publicPath: process.env.BASE_URL,
  outputDir: process.env.BUILD_PATH,
  chainWebpack: (config) => {
    // Modify an option of the html-webpack-plugin. https://cli.vuejs.org/guide/webpack.html#modifying-options-of-a-plugin
    config.plugin("html").tap((args) => {
      // This title value is used in public/index.html
      args[0].title = process.env.VUE_APP_WEB_PAGE_TITLE;
      return args;
    });
  },
  lintOnSave: false,
};
