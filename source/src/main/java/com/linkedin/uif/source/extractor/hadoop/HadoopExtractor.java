/* (c) 2014 LinkedIn Corp. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 */

package com.linkedin.uif.source.extractor.hadoop;

import java.io.IOException;
import java.util.Iterator;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericRecord;

import com.google.common.base.Throwables;
import com.linkedin.uif.configuration.ConfigurationKeys;
import com.linkedin.uif.configuration.WorkUnitState;
import com.linkedin.uif.source.extractor.filebased.FileBasedExtractor;
import com.linkedin.uif.source.extractor.filebased.FileBasedHelperException;


public class HadoopExtractor<S, D> extends FileBasedExtractor<S, D> {

  public HadoopExtractor(WorkUnitState workUnitState) {
    super(workUnitState, new HadoopFsHelper(workUnitState));
  }

  @Override
  public Iterator<D> downloadFile(String file)
      throws IOException {
    DataFileReader<GenericRecord> dfr = null;
    try {
      dfr = ((HadoopFsHelper) this.fsHelper).getAvroFile(file);
      fileHandles.put(file, dfr);
      return (Iterator<D>) dfr;
    } catch (FileBasedHelperException e) {
      Throwables.propagate(e);
    }
    return null;
  }

  /**
   * Assumption is that all files in the input directory have the same schema
   */
  @Override
  public S getSchema() {
    if (this.workUnit.contains(ConfigurationKeys.SOURCE_SCHEMA)) {
      return (S) this.workUnit.getProp(ConfigurationKeys.SOURCE_SCHEMA);
    }

    HadoopFsHelper hfsHelper = (HadoopFsHelper) this.fsHelper;
    if (this.filesToPull.isEmpty()) {
      return null;
    } else {
      try {
        return (S) hfsHelper.getAvroSchema(this.filesToPull.get(0));
      } catch (FileBasedHelperException e) {
        Throwables.propagate(e);
        return null;
      }
    }
  }
}
