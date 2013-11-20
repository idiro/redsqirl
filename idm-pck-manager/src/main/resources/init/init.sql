DROP TABLE IF EXISTS idm_pck_mng.idm_packages;

CREATE TABLE  idm_pck_mng.idm_packages (
  name VARCHAR(45) NOT NULL,
  version VARCHAR(45) NOT NULL,
  license VARCHAR(45) NOT NULL,
  price VARCHAR(45) NOT NULL,
  short_description VARCHAR(400) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  release_notes VARCHAR(2000) NOT NULL,
  package_date datetime NOT NULL,
  url VARCHAR(400) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
INSERT INTO idm_pck_mng.idm_packages( 
  name,
  version,
  license,
  price,
  short_description,
  description,
  release_notes,
  package_date,
  url)
VALUES(
  'idm-base-hive',
  '0.1',
  'Apache',
  '',
  'Query the Hive default database.',
  CONCAT('The package query the Hive default database. Three actions are included in the package. ',
  'The Select transform a Hive table, Join join several tables together and Union aggregate data sets. ',
  'A SQL knowledge is useful but not required for using this package'),
  'First release',
  now(),
  'http://localhost:8080/idm-pck-manager/test.txt'
  );
  
INSERT INTO idm_pck_mng.idm_packages( 
  name,
  version,
  license,
  price,
  short_description,
  description,
  release_notes,
  package_date,
  url)
VALUES(
  'idm-base-pig',
  '0.1',
  'Apache',
  '',
  'Manipulate Map-Reduce direcories.',
  CONCAT('The package supports basic Map-Reduce ETL. Three actions are included in the package. ',
  'The Select transform a Map-Reduce directory, Join join several directories together and Union aggregate data sets. ',
  'A SQL knowledge is useful but not required for using this package'),
  'First release.',
  now(),
  'http://localhost:8080/idm-pck-manager/test.txt'
  );
  
INSERT INTO idm_pck_mng.idm_packages( 
  name,
  version,
  license,
  price,
  short_description,
  description,
  release_notes,
  package_date,
  url)
VALUES(
  'idm-base-hive',
  '0.2',
  'Apache',
  '',
  'Query the Hive default database.',
  CONCAT('The package query the Hive default database. Three actions are included in the package. ',
  'The Select transform a Hive table, Join join several tables together and Union aggregate data sets. ',
  'A SQL knowledge is useful but not required for using this package'),
  'So much better than first release.',
  now(),
  'http://localhost:8080/idm-pck-manager/test.txt'
  );