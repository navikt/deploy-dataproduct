-- Changing the view definition in this file does not do anything - this is just a copy of the SQL used to create the view in BQ

WITH vera AS (
    SELECT
        application,
        version,
        deployed_timestamp,
        CASE WHEN SUBSTR(deployer, 0, 10)='naiserator' THEN REGEXP_EXTRACT(deployer, r'\((.*)\)') ELSE NULL END team
    FROM `nais-analyse-prod-2dcc.deploys.vera_deploy_history`
), devrapid AS (
    SELECT
        application,
        version,
        deployTime deployed_timestamp,
        team
    FROM `nais-analyse-prod-2dcc.deploys.from_devrapid`
    WHERE environment='production'
)

SELECT * FROM vera
UNION ALL
SELECT * FROM devrapid