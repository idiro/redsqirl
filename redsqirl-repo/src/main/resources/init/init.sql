DROP TABLE IF EXISTS redsqirl_pck_mng.redsqirl_packages;
DROP TABLE IF EXISTS redsqirl_pck_mng.redsqirl_hosts;

-- The id have to be shared through versions
CREATE TABLE  redsqirl_pck_mng.redsqirl_packages (
  id   VARCHAR(90) NOT NULL,
  name VARCHAR(90) NOT NULL,
  version VARCHAR(45) NOT NULL,
  license VARCHAR(45) NOT NULL,
  price VARCHAR(45) NOT NULL,
  short_description VARCHAR(400) NOT NULL,
  html_file VARCHAR(100) NOT NULL,
  release_notes VARCHAR(2000) NOT NULL,
  package_date datetime NOT NULL,
  zip_file VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 

CREATE TABLE  redsqirl_pck_mng.redsqirl_hosts (
  name VARCHAR(400) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO redsqirl_pck_mng.redsqirl_hosts (
    name)
 VALUES(
    'http://localhost:8080/redsqirl-pck-manager/'
    );