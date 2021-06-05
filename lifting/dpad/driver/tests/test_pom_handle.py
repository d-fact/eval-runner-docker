import unittest
import logging
from driver.mvn import Mvn

logger = logging.getLogger(__name__)


class TestModifyMvnPom(unittest.TestCase):
    def test_insert_debug_opt(self):
        pom_path: str = "tests/data/pom.xml"
        Mvn.modify_javac_opt(pom_path)
        # example
        # <plugins>
        # <plugin>
        # <groupId> org.apache.maven.plugins</groupId>
        # <artifactId> maven - compiler - plugin </artifactId>
        # <version>3.8.1</version>
        # <configuration>
        # <compilerArgs>
        # <arg>-verbose</arg >
        # <arg>-Xlint: all, -options, -path </arg>


if __name__ == '__main__':
    unittest.main()
