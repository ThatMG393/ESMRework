

es = {
    TimeScale = 1,
    DeltaTime = 0,
    FixedDeltaTime = 0,
    Multiblock = {
        Type = nil,
	    Rigidbody = {
            UseGravity = true
        },
        ModBlock = { }
    },
    Player = { },
    Quaternion = { }
}

-- Multiblock
function es.TryGetMultiBlockFromConnection(connectionIndex, isIncoming, count) end
-- function 
    
-- Sound
function es.SetSoundVolume(volume) end
function es.SetSoundLoop(value) end
function es.PlaySound(soundPath) end

-- Data
function es.GetBool(key, value) end
function es.GetFloat(key, value) end

-- Classes

-- Multiblock class implementation
function es.Multiblock.ModBlock.Send(data) end

-- Quaternion class implementation
function es.Quaternion.Euler(x, y, z) end
function es.Quaternion.LookRotation(v3f) end
function es.Quaternion.LookRotation(v3f, v3u) end
function es.Quaternion.ToEulerAngles(quaternion) end
function es.Quaternion.FromToRotation(v3from, v3to) end
