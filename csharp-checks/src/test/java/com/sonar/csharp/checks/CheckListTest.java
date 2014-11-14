/*
 * Sonar C# Plugin :: C# Squid :: Checks
 * Copyright (C) 2010 Jose Chillan, Alexandre Victoor and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.csharp.checks;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.fest.assertions.Assertions.assertThat;

public class CheckListTest {

  /**
   * Enforces that each check declared in list.
   */
  @Test
  public void count() {
    int count = 0;
    List<File> files = (List<File>) FileUtils.listFiles(new File("src/main/java/com/sonar/csharp/checks/"), new String[] {"java"}, false);
    for (File file : files) {
      if (file.getName().endsWith("Check.java")) {
        count++;
      }
    }
    assertThat(CheckList.getChecks().size()).isEqualTo(count);
  }

  /**
   * Enforces that each check has test, name and description.
   */
  @Test
  public void test() {
    List<Class> checks = CheckList.getChecks();

    ResourceBundle resourceBundle = ResourceBundle.getBundle("org.sonar.l10n.csharp", Locale.ENGLISH);

    List<Rule> rules = new AnnotationRuleParser().parse("repositoryKey", checks);
    for (Rule rule : rules) {
      resourceBundle.getString("rule.csharpsquid." + rule.getKey() + ".name");
      assertThat(getClass().getResource("/org/sonar/l10n/csharp/rules/csharpsquid/" + rule.getKey() + ".html"))
        .overridingErrorMessage("No description for " + rule.getKey())
        .isNotNull();

      assertThat(rule.getDescription())
        .overridingErrorMessage("Description of " + rule.getKey() + " should be in separate file")
        .isNullOrEmpty();

      for (RuleParam param : rule.getParams()) {
        resourceBundle.getString("rule.csharpsquid." + rule.getKey() + ".param." + param.getKey());

        assertThat(param.getDescription())
          .overridingErrorMessage("Description for param " + param.getKey() + " of " + rule.getKey() + " should be in separate file")
          .isNullOrEmpty();
      }
    }
  }

}
