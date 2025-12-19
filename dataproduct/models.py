from dataclasses import dataclass


@dataclass
class Resource:
    name: str
    kind: str

    def to_dict(self, deployment):
        return {
            "cluster": deployment.cluster,
            "deployTime": deployment.deployTime,
            "deploymentSystem": deployment.deploymentSystem,
            "environment": deployment.environment,
            "gitCommitSha": deployment.gitCommitSha,
            "namespace": deployment.namespace,
            "platform": deployment.platform,
            "resourceKind": self.kind,
            "resourceName": self.name,
            "team": deployment.team,
        }

    def __str__(self):
        return f"<Resource name={self.name} kind={self.kind}>"


@dataclass
class Deployment:
    def __init__(self, teamSlug: str, environmentName: str, createdAt: str, commitSha: str, resources: dict):
        self.platform = "nais"
        self.deploymentSystem = "Nais API"

        self.team = teamSlug
        self.environment = self._env_type(environmentName)
        self.namespace = teamSlug
        self.cluster = environmentName
        self.deployTime = createdAt
        self.gitCommitSha = commitSha

        self.resources = [Resource(**res) for res in resources['nodes']]

    def _env_type(self, env_name: str) -> str:
        if "dev" in env_name.lower():
            return "development"
        elif "nonprod" in env_name.lower():
            return "development"
        elif "prod" in env_name.lower():
            return "production"
        else:
            return "unknown"

    def to_dict(self):
        return [resource.to_dict(self) for resource in self.resources]
