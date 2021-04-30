from setuptools import setup, find_packages

setup(name='factutils',
      version='0.1',
      packages=find_packages(),
      install_requires=[
          'GitPython',
          'pyyaml'
      ])
