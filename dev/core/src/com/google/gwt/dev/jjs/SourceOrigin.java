/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jjs;

import com.google.gwt.dev.jjs.Correlation.Axis;
import com.google.gwt.dev.jjs.CorrelationFactory.DummyCorrelationFactory;
import com.google.gwt.dev.util.StringInterner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Describes where a SourceInfo's node came from. This class currently includes
 * only physical origin information, but could be extended to provide support
 * for source-Module and -Generators.
 * 
 * TODO: rename this class to make it parallel to {@link SourceInfoCorrelation}?
 * 
 * TODO: make this package-protected?
 */
public class SourceOrigin implements SourceInfo {

  private static class SourceOriginPos extends SourceOrigin {
    private final int endPos;
    private final int startPos;

    private SourceOriginPos(String location, int startLine, int startPos,
        int endPos) {
      super(location, startLine);
      this.startPos = startPos;
      this.endPos = endPos;
    }

    @Override
    public int getEndPos() {
      return endPos;
    }

    @Override
    public int getStartPos() {
      return startPos;
    }

    // super.equals and hashCode call getStartPos() and getEndPos(),
    // so there is no need to implement them in this subclass
  }

  public static final SourceInfo UNKNOWN = new SourceOrigin("Unknown", 0) {
    private Object readResolve() {
      return UNKNOWN;
    }
  };

  /**
   * Cache to reuse recently-created origins. This is very useful for JS nodes,
   * since {@link com.google.gwt.dev.js.JsParser} currently only provides line
   * numbers rather than character positions, so we get a lot of reuse there. We
   * get barely any reuse in the Java AST. Synchronized since several threads
   * could operate on it at once during parallel optimization phases.
   */
  private static final Map<SourceOrigin, SourceOrigin> CANONICAL_SOURCE_ORIGINS = Collections.synchronizedMap(new LinkedHashMap<SourceOrigin, SourceOrigin>(
      150, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Entry<SourceOrigin, SourceOrigin> eldest) {
      return size() > 100;
    }
  });

  /**
   * Creates SourceOrigin nodes.
   */
  public static SourceOrigin create(int startPos, int endPos, int startLine,
      String fileName) {
    if (startPos < 0 && endPos < 0) {
      return create(startLine, fileName);
    }

    return new SourceOriginPos(fileName, startLine, startPos, endPos);
  }

  /**
   * Creates SourceOrigin nodes. This factory method will attempt to provide
   * canonicalized instances of SourceOrigin objects.
   */
  public static SourceOrigin create(int startLine, String fileName) {

    SourceOrigin newInstance = new SourceOrigin(fileName, startLine);
    SourceOrigin canonical = CANONICAL_SOURCE_ORIGINS.get(newInstance);

    assert canonical == null
        || (newInstance != canonical && newInstance.equals(canonical));

    if (canonical != null) {
      return canonical;
    } else {
      CANONICAL_SOURCE_ORIGINS.put(newInstance, newInstance);
      return newInstance;
    }
  }

  // TODO: Add Module and Generator tracking
  private final String fileName;
  private final int startLine;

  private SourceOrigin(String location, int startLine) {
    this.fileName = StringInterner.get().intern(location);
    this.startLine = startLine;
  }

  public void addCorrelation(Correlation c) {
  }

  public void copyMissingCorrelationsFrom(SourceInfo other) {
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SourceOrigin)) {
      return false;
    }
    SourceOrigin other = (SourceOrigin) o;
    return startLine == other.startLine && getEndPos() == other.getEndPos()
        && getStartPos() == other.getStartPos()
        && fileName.equals(other.fileName);
  }

  public List<Correlation> getAllCorrelations() {
    return Collections.emptyList();
  }

  public List<Correlation> getAllCorrelations(Axis axis) {
    return Collections.emptyList();
  }

  public CorrelationFactory getCorrelationFactory() {
    return DummyCorrelationFactory.INSTANCE;
  }

  public int getEndPos() {
    return -1;
  }

  public String getFileName() {
    return fileName;
  }

  public SourceOrigin getOrigin() {
    return this;
  }

  public Correlation getPrimaryCorrelation(Axis axis) {
    return null;
  }

  public Set<Correlation> getPrimaryCorrelations() {
    return Collections.emptySet();
  }

  public Correlation[] getPrimaryCorrelationsArray() {
    return new Correlation[0];
  }

  public int getStartLine() {
    return startLine;
  }

  public int getStartPos() {
    return -1;
  }

  @Override
  public int hashCode() {
    return 2 + 13 * fileName.hashCode() + 17 * startLine + 29 * getStartPos()
        + 31 * getEndPos();
  }

  public SourceInfo makeChild(Class<?> caller, String description) {
    return this;
  }

  public SourceInfo makeChild(Class<?> caller, String description,
      SourceInfo... merge) {
    return this;
  }

  public SourceInfo makeChild(SourceOrigin origin) {
    return origin;
  }

  public void merge(SourceInfo... sourceInfos) {
  }

  @Override
  public String toString() {
    return getFileName() + '(' + getStartLine() + ')';
  }
}