ALTER TABLE teamkills
  ADD id INT NOT NULL PRIMARY KEY AUTO_INCREMENT;
ALTER TABLE teamkills
  MODIFY COLUMN id INT FIRST;

delete from teamkills where teamkiller not in (select id from login);
ALTER TABLE teamkills
  ADD CONSTRAINT teamkiller_fk
FOREIGN KEY (teamkiller) REFERENCES login (id);

delete from teamkills where victim not in (select id from login);
ALTER TABLE teamkills
  ADD CONSTRAINT victim_fk
FOREIGN KEY (victim) REFERENCES login (id);
