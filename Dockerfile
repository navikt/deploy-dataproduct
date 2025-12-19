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

WORKDIR /app

ENV PYTHONUNBUFFERED=1
ENV PATH="/app/.venv/bin:$PATH"

COPY schema.json .
COPY dataproduct.py .
COPY forwarder.py .
COPY dataproduct/ ./dataproduct
COPY --from=builder /app/.venv /app/.venv
