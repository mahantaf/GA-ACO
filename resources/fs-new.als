abstract sig FSObject {}
sig Dir extends FSObject {
    contents : set FSObject
}
sig File extends FSObject {}
one sig Root extends Dir {}

fact Hierarchy {
    no contents.Root
    FSObject in Root.*contents
    all obj: FSObject | lone contents.obj
}

pred model {
    some File
}

run model for 5 File, 5 Dir


