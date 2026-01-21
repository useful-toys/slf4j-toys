---
description: 'Guidelines for building Java base applications'
applyTo: '**/*.java'
---

# Java Programming Standards

This document contains all Java-specific programming guidelines for the slf4j-toys project.

## Code Standards

### Language & Style
- **English only**: All identifiers, strings, Javadocs, comments, documentation, and commit messages must be in English
- **Java 8+**: Code must be compatible with Java 8 or higher
- **Follow conventions**: Maintain consistency with existing code style
- **Immutability**: Declare variables, parameters, and attributes `final` whenever possible
- **Lombok usage**: Use Lombok annotations to reduce boilerplate
- **UTF-8 encoding**: All source files must be encoded in UTF-8
- **Comment style**: For comments explaining behavior or logic, prefer `/* ... */` style over `//` style
- **Copyright Header**: All Java files must include the following Apache 2.0 license header at the top, with the current year (2026) in the copyright notice:
  ```
  /*
   * Copyright 2026 Daniel Felix Ferber
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
  ```

### Javadoc Requirements
- **All classes and members (including `private` and package-private) must have clear Javadoc**
- **Do NOT document methods that implement third-party or well-documented interfaces**
- **Do NOT document overridden methods unless there are significant behavioral changes**
- Write clear, concise descriptions that explain the method's purpose and behavior
- Use proper Javadoc formatting with complete sentences ending in periods

> **Note**: For testing standards and guidelines, see [java-test.instructions.md](java-test.instructions.md).
