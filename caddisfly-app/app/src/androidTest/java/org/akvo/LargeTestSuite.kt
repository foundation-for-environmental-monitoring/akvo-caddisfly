package org.akvo

import org.akvo.caddisfly.test.StriptestInternalTest
import org.akvo.caddisfly.test.StriptestTest
import org.akvo.caddisfly.test.TitrationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(StriptestInternalTest::class, StriptestTest::class, TitrationTest::class)
class LargeTestSuite