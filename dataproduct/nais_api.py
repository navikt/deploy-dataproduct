from .models import Deployment
import requests

DEPLOYMENTS_QUERY = """query GetDeployments($filters: DeploymentFilter, $after: Cursor) {
  deployments (filter: $filters, after: $after, first: 500) {
    pageInfo {
      totalCount
      hasNextPage
      endCursor
    }
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

    deployments = []
    while True:
        try:
            response = requests.post("https://console.nav.cloud.nais.io/graphql",
                                     timeout=10,
                                     headers={
                                         "Authorization": "Bearer " + apikey},
                                     json={'query': DEPLOYMENTS_QUERY, 'variables': variables})
            response.raise_for_status()
        except requests.exceptions.RequestException as err:
            print("An error occurred:", err)
            exit(1)

        payload = response.json()

        for deployment in payload['data']['deployments']['nodes']:
            dep = Deployment(**deployment)
            deployments.extend(dep.to_dict())

        pageInfo = payload['data']['deployments']['pageInfo']
        print(
            f"Fetched {len(payload['data']['deployments']['nodes'])} deployments, total so far: {len(deployments)}, of total available: {pageInfo['totalCount']}")

        if pageInfo['hasNextPage']:
            variables['after'] = pageInfo['endCursor']
        else:
            break

    return deployments
