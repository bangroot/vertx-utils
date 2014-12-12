package com.github.bangroot.vertx.groovy

import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.vertx.groovy.platform.Verticle

import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.closeQuietly

abstract class ExtensionVerticle extends Verticle {

  static {
    /*
        this is probably dangerous, but I'll find a better way laterz TODO
        if anyone stumbles across this and has an idea, here's the issue:

        Vert.x loads the groovy module in a separate class loader from the implementation module which means
        any extensions in libraries included in the implementation module. I'd prefer not to use @Mixin all
        over or use() clauses. This code seems to apply module extensions properly after the fact at the
        expense of cleanliness and the use of ExpandoMetaClass.enableGlobally().
     */
    ExpandoMetaClass.enableGlobally()
    MetaClassRegistryImpl mcri = (MetaClassRegistryImpl) MetaClassRegistryImpl.getInstance(MetaClassRegistryImpl.LOAD_DEFAULT)
    println "Loading module extensions!"

    try {
      Enumeration<URL> resources = ExtensionVerticle.classLoader.getResources(ExtensionModuleScanner.MODULE_META_INF_FILE);
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        Properties properties = new Properties();
        InputStream inStream = null;
        try {
          inStream = url.openStream();
          properties.load(inStream);
          def map = [:]
          if (!mcri.moduleRegistry.hasModule(properties.moduleName)) {
            mcri.registerExtensionModuleFromProperties(properties, ExtensionVerticle.classLoader, map)

            for (Map.Entry<CachedClass, List<MetaMethod>> e : map.entrySet()) {
              CachedClass cls = e.getKey();
              cls.addNewMopMethods(e.getValue());
            }
          }
        } catch (Exception e) {
          System.err.println("Error loading extension. $e.message")
        } finally {
          closeQuietly(inStream);
        }
      }
    } catch (Exception e) {
      System.err.println("Error loading extension. $e.message")
    }
  }
}
