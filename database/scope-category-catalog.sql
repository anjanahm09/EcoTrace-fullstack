-- Reference category catalog for implementation/migration. This is not a full schema migration.
CREATE TABLE IF NOT EXISTS scope_category_catalog (
  id BIGINT PRIMARY KEY,
  scope_code VARCHAR(20) NOT NULL,
  category_number INT NULL,
  category_name VARCHAR(160) NOT NULL,
  description VARCHAR(500) NOT NULL,
  UNIQUE KEY uq_scope_category (scope_code, category_number)
);
INSERT IGNORE INTO scope_category_catalog (id,scope_code,category_number,category_name,description) VALUES
(1,'SCOPE_1',NULL,'Stationary combustion','Direct emissions from stationary fuel combustion'),
(2,'SCOPE_1',NULL,'Mobile combustion','Direct emissions from owned or controlled mobile sources'),
(3,'SCOPE_1',NULL,'Process emissions','Direct process emissions'),
(4,'SCOPE_1',NULL,'Fugitive emissions','Refrigerants and other fugitive emissions'),
(5,'SCOPE_2',NULL,'Purchased energy','Purchased electricity, steam, heating and cooling; location-based and market-based methods must remain separate'),
(101,'SCOPE_3',1,'Purchased goods and services','Upstream goods and services purchased in the reporting year'),
(102,'SCOPE_3',2,'Capital goods','Upstream emissions from capital goods'),
(103,'SCOPE_3',3,'Fuel- and energy-related activities','Not included in Scope 1 or Scope 2'),
(104,'SCOPE_3',4,'Upstream transportation and distribution','Upstream logistics and distribution'),
(105,'SCOPE_3',5,'Waste generated in operations','Third-party waste treatment and disposal'),
(106,'SCOPE_3',6,'Business travel','Employee business travel'),
(107,'SCOPE_3',7,'Employee commuting','Employee travel between home and work'),
(108,'SCOPE_3',8,'Upstream leased assets','Operation of upstream leased assets not in Scope 1/2'),
(109,'SCOPE_3',9,'Downstream transportation and distribution','Downstream logistics and distribution'),
(110,'SCOPE_3',10,'Processing of sold products','Processing of intermediate products by customers'),
(111,'SCOPE_3',11,'Use of sold products','Expected lifetime use of sold products'),
(112,'SCOPE_3',12,'End-of-life treatment of sold products','Waste treatment/disposal of sold products'),
(113,'SCOPE_3',13,'Downstream leased assets','Operation of downstream leased assets'),
(114,'SCOPE_3',14,'Franchises','Operation of franchises not in Scope 1/2'),
(115,'SCOPE_3',15,'Investments','Emissions associated with investments');
