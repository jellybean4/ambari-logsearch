/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ambari.logsearch.doc;

import org.apache.ambari.logsearch.config.api.LogSearchPropertyDescription;
import org.apache.ambari.logsearch.config.api.ShipperConfigElementDescription;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LogSearchMarkdownGenerator {

  public static void main(String[] args) {
    final Map<String, List<PropertyDescriptionData>> propertyDescriptions = new ConcurrentHashMap<>();
    final List<String> configPackagesToScan = Arrays.asList("org.apache.ambari.logsearch", "org.apache.ambari.logfeeder");
    fillPropertyDescriptions(propertyDescriptions, configPackagesToScan);
    System.out.println(propertyDescriptions);

    final List<ShipperConfigDescriptionData> shipperConfigDescription = new ArrayList<>();
    final List<String> shipperConfigPackagesToScan = Arrays.asList("org.apache.ambari.logsearch.config.json.model.inputconfig.impl");
    fillShipperConfigDescriptions(shipperConfigDescription, shipperConfigPackagesToScan);
    System.out.println(shipperConfigDescription);
  }

  private static void fillPropertyDescriptions(Map<String, List<PropertyDescriptionData>> propertyDescriptions, List<String> packagesToScan) {
    List<PropertyDescriptionData> propertyDescriptionsList = getPropertyDescriptions(packagesToScan);
    Map<String, List<PropertyDescriptionData>> mapToAdd = propertyDescriptionsList.stream()
      .sorted(Comparator.comparing(PropertyDescriptionData::getName))
      .collect(Collectors.groupingBy(PropertyDescriptionData::getSource));
    propertyDescriptions.putAll(mapToAdd);
  }

  private static void fillShipperConfigDescriptions(List<ShipperConfigDescriptionData> shipperConfigDescription, List<String> shipperConfigPackagesToScan) {
    Reflections reflections = new Reflections(shipperConfigPackagesToScan, new FieldAnnotationsScanner());
    Set<Field> fields = reflections.getFieldsAnnotatedWith(ShipperConfigElementDescription.class);
    for (Field field : fields) {
      ShipperConfigElementDescription description = field.getAnnotation(ShipperConfigElementDescription.class);
      shipperConfigDescription.add(new ShipperConfigDescriptionData(description.path(), description.description(),
        description.examples(), description.defaultValue()));
    }
    shipperConfigDescription.sort(Comparator.comparing(ShipperConfigDescriptionData::getPath));
  }

  private static List<PropertyDescriptionData> getPropertyDescriptions(List<String> packagesToScan) {
    List<PropertyDescriptionData> result = new ArrayList<>();
    for (String packageToScan : packagesToScan) {
      Reflections reflections = new Reflections(packageToScan, new FieldAnnotationsScanner(), new MethodAnnotationsScanner());
      Set<Field> fields = reflections.getFieldsAnnotatedWith(LogSearchPropertyDescription.class);
      for (Field field : fields) {
        LogSearchPropertyDescription propDescription = field.getAnnotation(LogSearchPropertyDescription.class);
        for (String source : propDescription.sources()) {
          result.add(new PropertyDescriptionData(propDescription.name(), propDescription.description(), propDescription.examples(), propDescription.defaultValue(), source));
        }
      }
      Set<Method> methods = reflections.getMethodsAnnotatedWith(LogSearchPropertyDescription.class);
      for (Method method : methods) {
        LogSearchPropertyDescription propDescription = method.getAnnotation(LogSearchPropertyDescription.class);
        for (String source : propDescription.sources()) {
          result.add(new PropertyDescriptionData(propDescription.name(), propDescription.description(), propDescription.examples(), propDescription.defaultValue(), source));
        }
      }
    }
    return result;
  }
}
