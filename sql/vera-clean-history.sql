-- Changing this SQL in the repo does nothing - this is just a copy of the SQL used to create the deploy history table

create table `nais-analyse-prod-2dcc.deploys.vera_deploy_history` as (
    select environment, application, version, deployer, deployed_timestamp, replaced_timestamp, environmentClass, id
    from `nais-analyse-prod-2dcc.deploys.vera-deploys`
    group by environment, application, version, deployer, deployed_timestamp, replaced_timestamp, environmentClass, id
)