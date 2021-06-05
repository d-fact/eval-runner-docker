from lxml import etree as ET
# import xml.etree.ElementTree as ET
import subprocess
import os
from pathlib import PurePath


class Pom:
    def __init__(self, pom_path):
        self.pom_path: str = pom_path
        pom_xmlns: str = ""


class Mvn:
    def __init__(self, repo_path, target_path="target", jar_package_file=None):
        """
            :repo_path (absolute path to git repo)
            :target_path (relative path to target dir, defaults to /target)
            :jar_package_file (jar file name, defaults to <artifactId>-<version>.jar)
        """
        self.repo_path = repo_path
        self.target_path = target_path

        if jar_package_file is not None:
            self.package_file = jar_package_file
        else:
            # if PurePath(repo_path).stem == "commons-net":
            tree = ET.parse(self.get_absolute_pom_path())
            root = tree.getroot()
            namespace = (root.tag.partition('}'))[0] + "}"
            self.artifact_id = self.get_artifact_id(root, namespace)
            self.version_id = self.get_version(root, namespace)

            if self.artifact_id == "commons-net":
                self.package_file = self.get_examples_jar()
            else:
                self.package_file = self.read_pom()

    def read_pom(self) -> str:
        return self.artifact_id + "-" + self.version_id + ".jar"

    def get_examples_jar(self) -> str:
        """return a jar name: aid-examples-vid.jar
        currently specially for commons-net
        :return the jar name for commons-net-examples-vid.jar
        """
        return f"{self.artifact_id}-examples-{self.version_id}.jar"

    def get_artifact_id(self, root, namespace):
        artifactId = root.find(namespace + "artifactId")
        if (artifactId is None):
            # TODO: throw errors if artifactId is not found
            pass
        else:
            return artifactId.text

    def get_version(self, root, namespace):
        version = root.find(namespace + "version")
        if version is None:
            # TODO: throw errors if version is not found
            pass
        else:
            return version.text

    def package(self):
        cdDir = "cd " + self.repo_path
        self.modify_javac_opt(self.get_absolute_pom_path())
        mvnPackage = "mvn package -Dmaven.test.skip=true"
        ret = subprocess.run(cdDir + " && " + mvnPackage, shell=True)

    def get_absolute_pom_path(self):
        return os.path.join(self.repo_path, "pom.xml")

    def get_absolute_jar_path(self):
        return os.path.join(self.repo_path, self.target_path, self.package_file)

    @staticmethod
    def modify_javac_opt(pom_path):
        # This method is intended as a temp workaround for tested target (commons-csv)
        # and will very possibly fail on lots of other projects
        # due to the complexity of XML-based pom files.
        tree = ET.parse(pom_path)
        root = tree.getroot()
        xmlns: str = (root.tag.partition('}'))[0] + "}"
        for tag_build in root.findall(f"{xmlns}build"):
            tag_plugin_col = tag_build.find(f"{xmlns}plugins")
            tag_plugin_mng = tag_build.find(f"{xmlns}pluginManagement")
            if tag_plugin_mng:
                tag_plugin_col = tag_plugin_mng.find(f"{xmlns}plugins")
            for tag_plugin in tag_plugin_col.findall(f"{xmlns}plugin"):
                a_id = tag_plugin.find(f"{xmlns}artifactId")
                # print(a_id.text)
                if a_id.text == "maven-compiler-plugin":
                    tag_config = tag_plugin.find(f"{xmlns}configuration")
                    if not tag_config:
                        tag_config = ET.SubElement(tag_plugin, 'configuration')
                    tag_compilerArgs = tag_config.find(f"compilerArgs")
                    if not tag_compilerArgs:
                        tag_compilerArgs = ET.SubElement(tag_config, 'compilerArgs')
                    dbg_opt = ET.Element("args")
                    dbg_opt.text = "-g"
                    tag_compilerArgs.insert(1, dbg_opt)
        tree.write(pom_path)
