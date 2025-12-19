# Dataprodukt for Nais-deployments

> Python jobb som henter deployments fra Nais API og putter de i BigQuery.

``` shell
gcloud auth login --update-adc
uv run python dataproduct.py
```

> Python app som forwarder deployments fra Vera til BigQuery.

``` shell
gcloud auth login --update-adc
uv run gunicorn forwarder:app
```

## Endring av BigQuery schema

1. Endre i filen `schema.json`
2. Kjør kommandoen `bq update --table --schema schema.json --project_id='nais-analyse-prod-2dcc' 'deploys.from_devrapid'`
