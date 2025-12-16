from .models import Deployment
import requests

DEPLOYMENTS_QUERY = """query GetDeployments($filters: DeploymentFilter, $after: Cursor) {
  deployments (filter: $filters, after: $after) {
    nodes {
      createdAt
      environmentName
      teamSlug
      commitSha
      resources {
        nodes {
          name
          kind
        }
      }
    }
  }
}"""


def get_deployments(apikey: str, since: str):
    variables = {
        'filters': {
            'from': since
        },
        'after': None
    }
    response = requests.post("https://console.nav.cloud.nais.io/graphql",
                             headers={"Authorization": "Bearer " + apikey},
                             json={'query': DEPLOYMENTS_QUERY, 'variables': variables})
    payload = response.json()

    deployments = []
    while True:
        for deployment in payload['data']['deployments']['nodes']:
            dep = Deployment(**deployment)
            deployments.extend(dep.to_dict())

        pageInfo = payload['data']['deployments']['pageInfo']
        if pageInfo['hasNextPage']:
            variables['after'] = pageInfo['endCursor']
        else:
            break

    return deployments
