/*
 *
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
 *
 *
 *
 * This interface is not used, but exists to demonstrate how interfaces are exported in typescript. Interfaces are used to map a model into a Typescript object.
 * For example, if the HelloWorldEndpoint contains a method for returning a specific object. An interface would be added to this file which contains all
 * the object's variable names and types. Then when response a is received on the frontend, a TypeScript object could be populated as follows:
 *
 * typescriptObject: exampleInterface = response.data
 */
export interface exampleInterface {
  textValue: string;
}
