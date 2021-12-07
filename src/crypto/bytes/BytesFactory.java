/*
 * Copyright 2017 Patrick Favre-Bulle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.qkernel.crypto.bytes;

import java.nio.ByteOrder;

/**
 * Simple factory for creating {@link Bytes} instances
 */
public interface BytesFactory {

    /**
     * Create an instance with given array and order
     *
     * @param array     to directly us
     * @param byteOrder the array is in
     * @return a new instance
     */
    Bytes wrap(byte[] array, ByteOrder byteOrder);
}
