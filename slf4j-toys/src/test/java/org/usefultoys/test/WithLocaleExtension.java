/*
 * Copyright 2025 Daniel Felix Ferber
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

package org.usefultoys.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.util.Locale;
import java.util.Optional;

public class WithLocaleExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Namespace NAMESPACE =
            Namespace.create(WithLocaleExtension.class);

    private static final String ORIGINAL_LOCALE_KEY = "originalLocale";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Salva o locale original
        Locale original = Locale.getDefault();
        getStore(context).put(ORIGINAL_LOCALE_KEY, original);

        // Descobre o locale desejado (método > classe)
        Optional<WithLocale> withLocale = findWithLocaleAnnotation(context);

        if (withLocale.isPresent()) {
            Locale newLocale = Locale.forLanguageTag(withLocale.get().value());
            Locale.setDefault(newLocale);
        } else {
            throw new IllegalStateException(
                    "@WithLocale annotation not found on test method or class.");
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Store store = getStore(context);
        Locale original = store.remove(ORIGINAL_LOCALE_KEY, Locale.class);

        if (original != null) {
            Locale.setDefault(original);
        }
    }

    private Store getStore(ExtensionContext context) {
        // Namespace só por classe de teste, para evitar conflitos
        return context.getStore(NAMESPACE);
    }

    private Optional<WithLocale> findWithLocaleAnnotation(ExtensionContext context) {
        // 1) Procura no método
        Optional<WithLocale> methodAnnotation = context.getTestMethod()
                .map(m -> m.getAnnotation(WithLocale.class));
        if (methodAnnotation.isPresent()) {
            return methodAnnotation;
        }

        // 2) Se não tiver no método, procura na classe
        return context.getTestClass()
                .map(c -> c.getAnnotation(WithLocale.class));
    }
}
