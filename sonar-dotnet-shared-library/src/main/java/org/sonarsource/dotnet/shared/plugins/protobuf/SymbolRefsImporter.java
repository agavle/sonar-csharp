/*
 * SonarSource :: .NET :: Shared library
 * Copyright (C) 2014-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.dotnet.shared.plugins.protobuf;

import static org.sonarsource.dotnet.shared.plugins.SensorContextUtils.toTextRange;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonarsource.dotnet.protobuf.SonarAnalyzer;
import org.sonarsource.dotnet.protobuf.SonarAnalyzer.SymbolReferenceInfo;

import java.util.HashMap;
import java.util.HashSet;

class SymbolRefsImporter extends ProtobufImporter<SonarAnalyzer.SymbolReferenceInfo> {

  private final SensorContext context;
  private final HashMap<InputFile, HashSet<SonarAnalyzer.SymbolReferenceInfo.SymbolReference>> fileSymbolReferences = new HashMap<>();

  SymbolRefsImporter(SensorContext context) {
    super(SonarAnalyzer.SymbolReferenceInfo.parser(), context, SonarAnalyzer.SymbolReferenceInfo::getFilePath);
    this.context = context;
  }

  @Override
  void consumeFor(InputFile inputFile, SymbolReferenceInfo message) {
    for (SonarAnalyzer.SymbolReferenceInfo.SymbolReference tokenInfo : message.getReferenceList()) {
      fileSymbolReferences
        .computeIfAbsent(inputFile, f -> new HashSet<>())
        .add(tokenInfo);
    }
  }

  @Override
  public void save() {
    for (HashMap.Entry<InputFile, HashSet<SonarAnalyzer.SymbolReferenceInfo.SymbolReference>> entry : fileSymbolReferences.entrySet()) {

      NewSymbolTable symbolTable = context.newSymbolTable().onFile(entry.getKey());

      for (SonarAnalyzer.SymbolReferenceInfo.SymbolReference tokenInfo : entry.getValue()) {
        NewSymbol symbol = symbolTable.newSymbol(toTextRange(entry.getKey(), tokenInfo.getDeclaration()));
        for (SonarAnalyzer.TextRange refTextRange : tokenInfo.getReferenceList()) {
          symbol.newReference(toTextRange(entry.getKey(), refTextRange));
        }
      }

      symbolTable.save();
    }
  }

  @Override
  boolean isProcessed(InputFile inputFile) {
    // we aggregate all symbol reference information, no need to process only the first protobuf file
    return false;
  }
}
