import json
import re
import math
import csv


def parse_results_obj():
    results = []

    fd = open("drawing.csv", "r")
    for x in fd:
        # get id and timestamp string
        id_timestamp_match = re.match("([0-9aA-zZ-]*,){3}", x)
        first_part = id_timestamp_match.group()
        result = {}

        # get id
        id_match = re.match("^([0-9]*)", first_part)
        id = id_match.group(1)
        if id == '':
            continue
        result['id'] = id_match.group(1)

        drawn_dots_json_str = x.replace(id_timestamp_match.group(), '')
        drawn_dots_json_str = drawn_dots_json_str[1:-2]
        result['stats'] = json.loads(drawn_dots_json_str)

        results.append(result)
        # print(result['stats']['username'])
    
    return results

results = parse_results_obj()
final_results = []

for case in results:
    total_speed = 0
    for i in range(1, len(case['stats']['dots_drawn'])):
        last_dot = case['stats']['dots_drawn'][i - 1]
        dot = case['stats']['dots_drawn'][i]

        dist = math.sqrt(pow(dot['x'] - last_dot['x'], 2) +  pow(dot['y'] - last_dot['y'], 2))
        time_diff = dot['timestamp'] - last_dot['timestamp']
        if time_diff > 1000:
            print('stop of 1000ms in ' + str(case['stats']['username']) + ' shape ' +
            str(case['stats']['game_type']) + ' of ' + str(time_diff))
        if time_diff == 0.0:
            speed = 0.0
        else:
            speed = dist / time_diff
        total_speed += speed

        # print(str(dot['id']) + " " + str(speed)) 

    avg_speed = total_speed / len(case['stats']['dots_drawn'])
    # print('type ' + str(case['stats']['username']) + ' shape ' + str(case['stats']['game_type']) +
    # ' total number ' + str(len(case['stats']['dots_drawn'])) + " avg_speed " + str(avg_speed)) 

    final_result = {}
    final_result['type'] = case['stats']['username']
    final_result['shape'] = case['stats']['game_type']
    final_result['time'] = case['stats']['results']['time']
    final_result['avg_speed (px/s)'] = avg_speed
    final_result['dots_count'] = len(case['stats']['dots_drawn'])
    final_results.append(final_result)

try:
    with open('speed_results.csv', 'w', newline='') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=['type', 'shape', 'time', 'avg_speed (px/s)', 'dots_count'])
        writer.writeheader()
        for data in final_results:
            writer.writerow(data)
except IOError:
    print("I/O error") 
