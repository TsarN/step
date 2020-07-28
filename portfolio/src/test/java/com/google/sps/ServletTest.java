// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletTest {
  protected final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalUserServiceTestConfig())
          .setEnvIsLoggedIn(true)
          .setEnvEmail("test-user@example.com")
          .setEnvAuthDomain("example.com")
          .setEnvAttributes(
              Collections.singletonMap(
                  "com.google.appengine.api.users.UserService.user_id_key",
                  "test_user"
              )
          );

  protected HttpServletRequest request;
  protected HttpServletResponse response;
  protected PrintWriter writer;
  protected StringWriter stringWriter;

  @Before
  public void setUp() throws Exception {
    helper.setUp();

    this.request = mock(HttpServletRequest.class);
    this.response = mock(HttpServletResponse.class);

    this.stringWriter = new StringWriter();
    this.writer = new PrintWriter(this.stringWriter);
    when(this.response.getWriter()).thenReturn(this.writer);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}
