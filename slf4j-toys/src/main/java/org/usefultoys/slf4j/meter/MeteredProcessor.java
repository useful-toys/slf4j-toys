/*
 * Copyright 2019 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SuppressWarnings("FieldCanBeLocal")
@SupportedAnnotationTypes("org.usefultoys.slf4j.meter.Metered")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(Processor.class)
public class MeteredProcessor extends AbstractProcessor {

    private int tally;
    private Trees trees;
    private TreeMaker make;
    private Names names;

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        trees = Trees.instance(env);
        final Context context = ((JavacProcessingEnvironment) env).getContext();
        make = TreeMaker.instance(context);
        names = Names.instance(context);
        tally = 0;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if(roundEnv.processingOver()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Finished looking for @"+Metered.class.getSimpleName()+" annotations");
            return false;
        }
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Metered.class);
        for (final Element each : elements) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found: "+ each.getKind().toString()+" "+each.getSimpleName());
            if (each.getKind() != ElementKind.METHOD) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Only methods are supported.");
                continue;
            }
            final JCTree.JCMethodDecl methodTree = (JCTree.JCMethodDecl) trees.getTree(each);
            final JCTree.JCTry tryStatement = make.Try(methodTree.body, List.<JCTree.JCCatch>nil(), make.Block(0, methodTree.body.stats));
            methodTree.body = make.Block(0, List.<JCTree.JCStatement>of(tryStatement));
        }
        return false;
    }
}
