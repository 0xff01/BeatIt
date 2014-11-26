% init data

path = "../mp3/04_Track__4.wav";

% init
bpmmax = 200; % 150
bpmmin = 50;

% analyze 8 seconds
tprocess = 8;

% read file
[y, sampleRate] = wavread(path);

% copy data
numSamples = (tprocess+1) * sampleRate;
data = zeros(numSamples, 1);
channels = columns(y);
for i=1:tprocess * sampleRate
  for c=1:channels
    data(i) = data(i)+y(i, c);
  end
end

% rms
rmsRate = 500;
num_rms_samples = int32(tprocess*rmsRate);
rms = zeros(num_rms_samples+1, 1);
for r=1:num_rms_samples+1

  t0 = (r-1)/rmsRate;
  t1 = r/rmsRate;
    
  i0 = int32(floor(t0 * sampleRate));
  i1 = int32(ceil(t1 * sampleRate));
  
  n = 0;
  for ii = i0:i1
    if(ii > 0)
      if(ii <= numSamples)
        rms(r) = rms(r) + data(ii)*data(ii);
        n++;
      end;
    end;
  end;
  
  rms(r) = rms(r)/n;
  rms(r) = sqrt(rms(r));
end;

%rms = diff(rms);

% correlation:
dt = 1/rmsRate;
tmin = 1/(bpmmax / 60);
tmax = 1/(bpmmin / 60);
numShifts = ceil((tmax-tmin) / dt);
tvec = linspace(tmin, tmax, numShifts);
divec = floor(linspace(tmin/dt, tmax/dt, numShifts));
bpmvec = 60./tvec;

% maximal shift:
maxshift = max(divec);
correlationSteps = num_rms_samples - maxshift;

c = zeros(numShifts, 1);

count = zeros(numShifts, 1);

for i=1:numShifts

  ct = 0;
  di = divec(i);
  dti = tvec(i);
  
  for j=1:correlationSteps
    ct = ct + rms(j)*rms(di+j);
  end;
  
  c(i) = ct;
end;

[maxval, maxi] = max(c);
bpm = bpmvec(maxi);

% first beat
fbeat = bpm / 60;
numShifts = int32(ceil(rmsRate / fbeat));
correlationSteps = num_rms_samples - numShifts;
comp = zeros(numShifts, 1);
count = zeros(numShifts, 1);
dt = 1/fbeat;
rmsValuesPerStep = numShifts;

for i = 1:numShifts

  ii = i;
  while 1
    
    if(ii<=num_rms_samples)
      comp(i) = comp(i) + rms(ii);
      count(i) = count(i) + 1;
    else
      break;
    end;
    
    ii = ii+rmsValuesPerStep;
  end;
  
  if(count(i) > 1)
    comp(i) = comp(i) / count(i);
  end
end;

[maxval, maxi] = max(comp);
first_beat = maxi/rmsRate;

% plot autocorrelation
title('Auto correlation');
%set(gca(), 'fontsize', 15);
hold on;
xlabel('bpm');
hold on;
ylabel('auto-correlation');
hold on;
plot(bpmvec, c);
hold on;
print('out/auto-correlation.png');