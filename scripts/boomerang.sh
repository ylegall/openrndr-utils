ffmpeg -i "$1" -filter_complex "[0]reverse[r];[0][r]concat,loop=0.5:720,setpts=N/30/TB" output.mp4
