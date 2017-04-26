

-- ----------------------------
-- Records of auth_role
-- ----------------------------

INSERT INTO `auth_role` (id,create_time,description,name,status,type,uuid)  VALUES ('1',null, '系统管理员', 'SYSTEM', '0', '3', '0a933cfa-0170-44db-bc05-315cbaa01aa5');
INSERT INTO `auth_role` (id,create_time,description,name,status,type,uuid)  VALUES ('2',null, '产品管理员', 'product_admin', '0', '2','d2314e4f-2cca-43f6-a420-58c6a2d2fd42');
INSERT INTO `auth_role` (id,create_time,description,name,status,type,uuid)  VALUES ('3',null, '普通管理员', 'ordinary_admin', '0', '1', 'a772eff6-5916-4183-99b5-d5adccacf2c7');
INSERT INTO `auth_role` (id,create_time,description,name,status,type,uuid)  VALUES ('4',null, null, 'test', '0', '0', 'f91369d3-2ef7-40cd-8853-c83ea5553c19');


-- ----------------------------
-- Records of auth_manager
-- ----------------------------

INSERT INTO `auth_manager` (id,access_key,create_manager_id,create_time,name,password,phone,product_id,role_id,secret_key,status,uuid) VALUES ('1', 'VE1EBAA01514AE4E79', null ,'2015-10-28 16:24:02', 'dingran', '47ec2dd791e31e2ef2076caf64ed9b3d', '13311335930',null ,'1', '08a5378e057e60914bbe1b6be6e0b793', '2','a4714859-5a9d-4d8d-bab7-1ef4fee5a7fb');
INSERT INTO `auth_manager` (id,access_key,create_manager_id,create_time,name,password,phone,product_id,role_id,secret_key,status,uuid) VALUES ('2', 'VE03CC63D61DCF56E5', '1', '2015-11-03 16:08:28', 'EJiaJiao@admin', 'e10adc3949ba59abbe56e057f20f883e', '13311335931', '1', '2', 'ff237b0209a94b21ee826f2bac9658b2', '2',  'd4ae37ce-24a8-4763-82b5-51b261a416c8');


-- ----------------------------
-- Records of auth_enterprise
-- ----------------------------

INSERT INTO `auth_enterprise` (id,app_id,app_key,enterprise_type,type) VALUES ('1', 'wxa0e62d03b3d15ea7', 'cafbf22874d7cce1eba7e53635c850d4', '0', '1');


-- ----------------------------
-- Records of auth_product
-- ----------------------------

INSERT INTO `auth_product` (id,is_many_equipment_login,description,name,type,uuid,domain) VALUES ('1', '0', '易家教', '易家教', 'EJiaJiao', '32bdd1c5-8eef-4843-9a9a-228dc0f3b617', 'e-eduspace.com');

