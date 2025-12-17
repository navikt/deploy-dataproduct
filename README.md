# Dataprodukt for Nais-deployments

``` shell
gcloud auth login --update-adc
uv run python dataproduct.py
```

## Endring av BigQuery schema

1. Endre i filen `schema.json`
2. Kjør kommandoen `bq update --table --schema schema.json --project_id='nais-analyse-prod-2dcc' 'deploys.from_devrapid'`
