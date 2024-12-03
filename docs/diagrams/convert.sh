#!/bin/bash

# Check if mmdc is installed
if ! command -v mmdc &>/dev/null; then
  echo "Error: Mermaid CLI (mmdc) is not installed."
  echo "Install it using: npm install -g @mermaid-js/mermaid-cli"
  exit 1
fi

# Directory for output images
OUTPUT_DIR="img"

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Process all .mmd files in the current directory
for file in *.mmd; do
  if [ -f "$file" ]; then
    # Extract the filename without extension
    filename=$(basename "$file" .mmd)

    # Convert .mmd to .png
    mmdc -i "$file" -o "$OUTPUT_DIR/${filename}.png"

    # Check if conversion was successful
    if [ $? -eq 0 ]; then
      echo "Successfully converted: $file -> $OUTPUT_DIR/${filename}.png"
    else
      echo "Failed to convert: $file"
    fi
  fi
done

echo "All conversions completed. PNG files are in the '$OUTPUT_DIR' directory."
