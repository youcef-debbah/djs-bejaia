/*
 * Handcrafted with love by Youcef DEBBAH
 * Copyright 2019 youcef-debbah@hotmail.com
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

package dz.ngnex.bean;

import dz.ngnex.entity.DatabaseEntity;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public final class IntegrityException extends Exception {
  private static final long serialVersionUID = -7652778072145492863L;
  private final String userMessageKey;
  private final String param;

  public IntegrityException(String message, String userMessageKey) {
    super(message);
    this.userMessageKey = userMessageKey;
    this.param = null;
  }

  public IntegrityException(String s, String userMessageKey, Throwable throwable) {
    super(s, throwable);
    this.userMessageKey = userMessageKey;
    this.param = null;
  }

  public IntegrityException(String message, String userMessageKey, String param) {
    super(message);
    this.userMessageKey = userMessageKey;
    this.param = param;
  }

  public IntegrityException(String message, String userMessageKey, String param, Throwable throwable) {
    super(message, throwable);
    this.userMessageKey = userMessageKey;
    this.param = param;
  }

  public String getUserMessageKey() {
    return userMessageKey;
  }

  public String getParam() {
    return param;
  }

  public static IntegrityException newIdentifierUsedException() {
    return newIdentifierUsedException(null, null);
  }

  public static IntegrityException newIdentifierUsedException(String identifier) {
    return newIdentifierUsedException(identifier, null);
  }

  public static IntegrityException newIdentifierUsedException(String identifier, Class<? extends DatabaseEntity> entityType) {
    if (entityType != null)
      return new IntegrityException("identifier already used for " + entityType.getSimpleName() + ": " + identifier, "identifierUsed");
    else
      return new IntegrityException("identifier already used: " + identifier, "identifierUsed");
  }
}
