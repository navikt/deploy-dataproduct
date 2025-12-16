from google.cloud import bigquery
from google.cloud.bigquery import DatasetReference, TableReference

# Define your query
LATEST_DEPLOY_QUERY = """
    SELECT *
    FROM `nais-analyse-prod-2dcc.deploys.from_devrapid`
    WHERE deployTime >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 7 DAY)
    ORDER BY deployTime DESC
    LIMIT 1
"""


class BigQueryClient:
    def __init__(self, project):
        self.project = project
        self.dataset_id = 'deploys'
        self.table_id = 'from_devrapid'

        self.client = bigquery.Client(project=self.project)
        self.table = self._ensure_table()

    def get_latest_deploy(self):
        query_job = self.client.query(LATEST_DEPLOY_QUERY)
        results = query_job.result()
        for row in results:
            return row.deployTime.isoformat()
        return None

    def push_rows(self, rows, dry_run=False):
        if dry_run:
            print(f"Dry run enabled. Would push {len(rows)} rows to BigQuery.")
            return

        errors = self.client.insert_rows(self.table, rows)
        if errors:
            print("Errors occurred while inserting rows: ", errors)
        else:
            print(
                f"Successfully inserted {len(rows)} rows into {self.table_id}.")

    def _ensure_table(self):
        dataset_ref = bigquery.DatasetReference(self.project, self.dataset_id)
        table_ref = bigquery.TableReference(dataset_ref, self.table_id)

        schema = [
            bigquery.SchemaField(
                name="platform", field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(
                name="deploymentSystem", field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(
                name="team", field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(name="environment",
                                 field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(
                name="namespace", field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(
                name="cluster", field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(
                name="deployTime", field_type="TIMESTAMP", mode="NULLABLE"),
            bigquery.SchemaField(name="gitCommitSha",
                                 field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(name="resourceName",
                                 field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(name="resourceKind",
                                 field_type="STRING", mode="NULLABLE"),

            # Unused fields, but kept for history
            bigquery.SchemaField(name="correlationId",
                                 field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(name="application",
                                 field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(
                name="version", field_type="STRING", mode="NULLABLE"),
            bigquery.SchemaField(name="containerImage",
                                 field_type="STRING", mode="NULLABLE"),
        ]

        return self.client.create_table(bigquery.Table(
            table_ref, schema=schema), exists_ok=True)
