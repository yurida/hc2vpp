#!/bin/bash
#
# Copyright (c) 2019 Cisco and/or its affiliates.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# $1 config element for <copy-config> RPC
# $2 expected running config
# $3 xPath to verify config against

DIR_NAME=$(dirname $0)

${DIR_NAME}/edit_config.py $1 -c
${DIR_NAME}/get_config.py --reply_filename _actual_config.xml

# fixme: find better xml comparison tool
# xmldiffs does not work well when difference occurs on deep level
${DIR_NAME}/xmldiffs.py $2 _actual_config.xml $3
ret_code=$?

if [[ ${ret_code} == 0 ]]; then
    echo "<edit-config> successful"
    rm _actual_config.xml
    exit 0
fi

echo "<edit-config> failed"
exit 1
