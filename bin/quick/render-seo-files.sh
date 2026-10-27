#!/bin/sh

set -eu

target_dir=${1:-}
config_file=${2:-}

if [ -z "$target_dir" ] || [ -z "$config_file" ]; then
    echo "Usage: $0 <static-directory> <application.properties>" >&2
    exit 1
fi

if [ ! -d "$target_dir" ] || [ ! -f "$config_file" ]; then
    echo "SEO render input does not exist: $target_dir / $config_file" >&2
    exit 1
fi

site_url=${SEO_SITE_URL_OVERRIDE:-$(sed -n 's/^[[:space:]]*seo\.site-url[[:space:]]*=[[:space:]]*//p' "$config_file" | tail -n 1)}
site_url=${site_url%/}

case "$site_url" in
    http://*|https://*) ;;
    *) echo "Invalid seo.site-url in $config_file: $site_url" >&2; exit 1 ;;
esac

find "$target_dir" -type f \( -name '*.html' -o -name '*.xml' -o -name 'robots.txt' \) -print | while IFS= read -r file; do
    if grep -q '__SEO_SITE_URL__' "$file"; then
        temp_file="${file}.seo-tmp"
        sed "s|__SEO_SITE_URL__|${site_url}|g" "$file" > "$temp_file"
        mv "$temp_file" "$file"
    fi
done

echo "SEO static URLs rendered with: $site_url"
