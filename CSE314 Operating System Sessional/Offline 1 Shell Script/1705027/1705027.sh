#!/bin/bash

#checking input file name
check_input_file(){
  if [ -f "$1" ]; then
    filename=$1
    return
  else
    echo -n "Enter valid input file name: "
    read filename
    while [[ ! -f $filename ]]; do
      echo -n "Enter valid input file name: "
      read filename
    done
  fi
}

#output to csv
create_csv(){
  echo "file type", "number of files" > "$createfolderpath/output_csv.csv"
  cd "$1"

  for f in *
  do
    if [[ -d "$f" ]] ; then
      x=`find $f -type f | wc -l`
      x=$((x-1))
      echo "$f","$x" >> "$createfolderpath/output_csv.csv"
    fi
  done

  echo "ignored","$ignored" >> "$createfolderpath/output_csv.csv"

  cd "$rootDirectory"
}

#search in array
search_arr(){
  #echo "here : $1"
  ext="$1"
  for i in "${!arr[@]}" ;
  do
      igExt="${arr[$i]}"
      #echo $igExt | wc
      #echo $ext | wc
      #echo "$ext :  $igExt"
      if [ "$ext" = "$igExt" ] ; then
        #echo "found here"
        return 0;
      fi
  done
  return 1;
}


#searching in directory
directory_tree(){
  cd "$1"

  for f in *
  do
    if [[ -d "$f" ]]; then
      x=$(($2+1))

      directory_tree "$f" $x
    elif [[ -f "$f" ]]; then

      if [[ $f == *.* ]]; then
        file_ext=${f##*.}
      else
        file_ext="others"
      fi
      #echo "outside : $file_ext"

      search_arr "$file_ext"
      ret=$?
      #echo "return : $ret"
      if (( $ret == 1 )) ; then
        #echo "inside condition : $file_ext"
        #echo $(pwd)
        if [[ ! -d "$OutputDirectory/$file_ext" ]]; then
          mkdir "$OutputDirectory/$file_ext" #making directory of extension
          > "$OutputDirectory/$file_ext/desc_$file_ext.txt" #creating txt file
        fi

        if [[ ! -f  "$OutputDirectory/$file_ext/$f" ]]; then
          cp "$(pwd)/$f" "$OutputDirectory/$file_ext/" #copying the file
          #echo "$(pwd)/$f" >> "$rootDirectory/$OutputDirectory/$file_ext/desc_$file_ext.txt"
          realpath --relative-to="$rootDirectory" "$(pwd)/$f" >> "$OutputDirectory/$file_ext/desc_$file_ext.txt"
        fi
      else
        ignored=$((ignored+1)) #counting ignored file
      fi
    fi
  done

  cd ../
}

#checking command line arguements
echo "Number of arguements : $#"
if (($# == 0)); then
  echo "Please provide arguements."
  echo "./script_name.sh [working directory name optional] input_file_name.txt"
  exit
elif (($# ==1)); then
  check_input_file "$1"
  workingDir=$(pwd)
elif (($# ==2)); then
  check_input_file "$2"
  workingDir=$1
else
  echo "please provide only working directory name and input file name"
  echo "./script_name.sh [working directory name optional] input_file_name.txt"
  exit
fi

#reading from input file. saving ignore files extension in array
n=0

OldIFS=($IFS)

while IFS=$'\r\n' read -r line; do
  arr[$n]=$line
  n=$((n+1))
done < "$filename"

IFS=$OldIFS

#echo ${arr[*]}

#making output directory
rootDirectory=$(pwd)
#echo $rootDirectory

cd "$workingDir"
cd ..
createfolderpath=$(pwd)
#echo $createfolderpath

cd "$rootDirectory"

mkdir -p "$createfolderpath/Output_Directory"
OutputDirectory="$createfolderpath/Output_Directory"
#echo $OutputDirectory
#echo $filename
#echo $workingDir
ignored=0

directory_tree "$workingDir" 1

create_csv "$OutputDirectory"
