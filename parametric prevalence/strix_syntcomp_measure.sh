#!/bin/bash

# Set the folder path
folder="$HOME/LTL-Synth/"

# Define the output CSV file
csv_file="strix_execution_times.csv"

# Create the CSV file with headers
echo "File Name,Execution Time (seconds)" > "$csv_file"

# Loop over files in the folder
for file_path in "$folder"*
do
    if [ -f "$file_path" ]; then
        # Extract the file name
        file_name=$(basename "$file_path")

        echo "Processing file: $file_name"

        # Execute the command with timeout and measure the execution time
        start_time=$(date +%s.%N)
        timeout 1m scripts/strix_tlsf.sh "$folder""$file_name" --realizability > /dev/null 2>&1
        exit_status=$?
        end_time=$(date +%s.%N)

        # Calculate the execution time or set to "Timeout" if timeout occurred
        if [ $exit_status -eq 124 ]; then
            execution_time="Timeout"
        else
            execution_time=$(echo "$end_time - $start_time" | bc)
        fi

        # Append the data to the CSV file
        echo "$file_name,$execution_time" >> "$csv_file"

        echo "Execution time for $file_name: $execution_time seconds"
        echo
    fi
done

