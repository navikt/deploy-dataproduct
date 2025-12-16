FROM cgr.dev/chainguard/python:latest-dev AS builder

ENV LANG=C.UTF-8
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH="/linky/venv/bin:$PATH"

WORKDIR /linky

RUN pip install uv

WORKDIR /app
COPY ./pyproject.toml ./
COPY ./uv.lock ./
RUN uv sync

FROM cgr.dev/chainguard/python:latest

WORKDIR /linky

ENV PYTHONUNBUFFERED=1
ENV PATH="/venv/bin:$PATH"

COPY dataproduct.py .
COPY dataproduct/ ./dataproduct/
COPY --from=builder /app/.venv /venv

ENTRYPOINT [ "python", "/linky/dataproduct.py" ]
