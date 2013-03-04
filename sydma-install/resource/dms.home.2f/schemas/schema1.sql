CREATE TABLE `Sensor_info` (
`sn_parameter` varchar(20) primary key,
`Sensor_sn` varchar(20),
`Parameter_unit` varchar(20));
CREATE TABLE `Logger_info` (
`Logger_sn` varchar(20) not null,
`sn_parameter_str` varchar(200),
`Site_name_abbr` varchar(10) not null,
`Logger_type` varchar(20), CONSTRAINT PK_Logger_info PRIMARY KEY (Logger_sn,Site_name_abbr));
CREATE TABLE `Download_info` (
`File_start_date` varchar(20),
`File_finish_date` varchar(20),
`Logger_sn` varchar(20),
`Time_change` integer,
`Upload_date` varchar(100),
`File_name` varchar(100),
`Hobo_dtf_name` varchar(100));
CREATE TABLE `Parameter_info` (
`Parameter_abbr` varchar(2),
`Parameter_full_name` varchar(30));
CREATE TABLE `Continuous_data` (
`Date_time` DATETIME primary key);
CREATE TABLE `ICT_time_correction` (
`File_name` varchar(50) primary key,
`Time` integer);
CREATE TABLE `Tensiometer_hole` (
`Site_name_abbr` varchar(10) not null,
`Hole_label` varchar(1) not null,
`Hole_depth` integer,
`Hole_pos_paral` varchar(1),
`Hole_pos_right` varchar(1), constraint pk_tensiometer_hole primary key (Site_name_abbr,Hole_label));
CREATE TABLE `SM_depth` (
`Sensor_sn` integer primary key,
`Depth` integer,
`Status` varchar(1));
CREATE TABLE `Tensiometer_month_exclude` (
`Sensor_sn` varchar(40) not null,
`Year` integer not null,
`Month` varchar(40) not null);
CREATE TABLE `particle_size_analysis` (
`ID` varchar(40) primary key,
`Site_name_abbr` varchar(40),
`Bulk_depth` varchar(40),
`Sample_weight`  numeric(18,5),
`Gravel_weight`  numeric(18,5),
`Tin_weight`  numeric(18,5),
`Tin_wet_soil`  numeric(18,5),
`Tin_dry_soil`  numeric(18,5),
`PSA_start_weight`  numeric(18,5),
`T5`  numeric(18,5),
`Hyd_5_254`  numeric(18,5),
`Hyd_5_284148`  numeric(18,5),
`Hyd_5_A8276`  numeric(18,5),
`T_8`  numeric(18,5),
`Hyd_8_254`  numeric(18,5),
`Hyd_8_284148`  numeric(18,5),
`Hyd_8_A8276`  numeric(18,5),
`Beaker_tare`  numeric(18,5),
`Coarse_sand`  numeric(18,5),
`Fine_sand`  numeric(18,5));
CREATE TABLE `pH_EC` (
`Site_name_abbr` varchar(40),
`Bulk_depth` varchar(40),
`Measurement` integer,
`Batch` integer,
`Sample` integer,
`Sample_weight` numeric(18,5),
`pH_dw` numeric(18,5),
`pH_CaCl2` numeric(18,5),
`EC` numeric(18,5),
`EC_temperature` numeric(18,5), constraint pk_pH_EC primary key (Sample,Measurement));
CREATE TABLE `DBH` (
`Tree` integer primary key,
`Site_name_abbr` varchar(40),
`DBH` decimal(10,2),
`Common_abbr` varchar(40),
`Date_time` DATETIME);
CREATE TABLE `Species` (
`Common_abbr` varchar(5),
`Common_full` varchar(60),
`Species_name` varchar(60) primary key);
CREATE TABLE `svp` (
`Site_name_abbr` varchar(10),
`Tree_tag` integer,
`DBH` numeric(18,5),
`Bark_N` integer,
`Bark_S` integer,
`Bark_E` integer,
`Bark_W` integer,
`Needle_aspect` integer,
`Unit_height` integer, constraint pk_SVP primary key (Site_name_abbr,Tree_tag));
CREATE TABLE `SVP_site_species` (
`Site_name_abbr` varchar(10) primary key,
`Common_abbr` varchar(10));
CREATE TABLE `Site_details` (
`Site_name_abbr` varchar(10) primary key,
`Latitude` numeric(18,5),
`Longitude` numeric(18,5),
`Slope` integer,
`Elevation` integer,
`Aspect` integer,
`Area` integer);
